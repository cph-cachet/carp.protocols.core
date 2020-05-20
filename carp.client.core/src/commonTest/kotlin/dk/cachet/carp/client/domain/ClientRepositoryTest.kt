package dk.cachet.carp.client.domain

import dk.cachet.carp.common.UUID
import dk.cachet.carp.deployment.application.DeploymentService
import dk.cachet.carp.deployment.application.DeploymentServiceHost
import dk.cachet.carp.deployment.infrastructure.InMemoryAccountService
import dk.cachet.carp.deployment.infrastructure.InMemoryDeploymentRepository
import dk.cachet.carp.protocols.domain.devices.SmartphoneDeviceRegistration
import dk.cachet.carp.test.runBlockingTest
import kotlin.test.*


interface ClientRepositoryTest
{
    /**
     * Called for each test to create a repository to run tests on.
     */
    fun createRepository(): ClientRepository

    private fun createDependencies(): Pair<ClientRepository, DeploymentService>
    {
        val deploymentService = DeploymentServiceHost( InMemoryDeploymentRepository(), InMemoryAccountService() )
        return Pair( createRepository(), deploymentService )
    }

    private suspend fun addTestDeployment( deploymentService: DeploymentService ): UUID
    {
        val protocol = createSmartphoneStudy()
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )

        return status.studyDeploymentId
    }


    @Test
    fun deviceRegistration_is_initially_null()
    {
        val (repo, _) = createDependencies()
        assertNull( repo.deviceRegistration )
    }

    @Test
    fun addStudyRuntime_can_be_retrieved() = runBlockingTest {
        val (repo, deploymentService) = createDependencies()
        val deploymentId = addTestDeployment( deploymentService )
        val roleName = smartphone.roleName
        val studyRuntime =
            StudyRuntime.initialize( deploymentService, deploymentId, roleName, smartphone.createRegistration() )

        repo.addStudyRuntime( studyRuntime )

        // Runtime can be retrieved by ID.
        val retrievedRuntime = repo.getStudyRuntimeBy( deploymentId, roleName )
        assertNotNull( retrievedRuntime )

        // Runtime is included in list.
        val allRuntimes = repo.getStudyRuntimeList()
        assertEquals( 1, allRuntimes.count() )
        assertNotNull( allRuntimes.single { it.studyDeploymentId == deploymentId && it.device.roleName == roleName } )
    }

    @Test
    fun addStudyRuntime_fails_for_existing_runtime() = runBlockingTest {
        val (repo, deploymentService) = createDependencies()
        val deploymentId = addTestDeployment( deploymentService )
        val roleName = smartphone.roleName
        val studyRuntime =
                StudyRuntime.initialize( deploymentService, deploymentId, roleName, smartphone.createRegistration() )
        repo.addStudyRuntime( studyRuntime )

        assertFailsWith<IllegalArgumentException> { repo.addStudyRuntime( studyRuntime ) }
    }

    @Test
    fun getStudyRuntimeBy_is_null_for_unknown_runtime()
    {
        val (repo, _) = createDependencies()

        val unknownId = UUID.randomUUID()
        assertNull( repo.getStudyRuntimeBy( unknownId, "Unknown" ) )
    }

    @Test
    fun getStudyRuntimeList_is_empty_initially()
    {
        val (repo, _) = createDependencies()

        assertEquals( 0, repo.getStudyRuntimeList().count() )
    }

    @Test
    fun updateStudyRuntime_succeeds() = runBlockingTest {
        val (repo, deploymentService) = createDependencies()
        val protocol = createDependentSmartphoneStudy()
        val snapshot = protocol.getSnapshot()
        val status = deploymentService.createStudyDeployment( snapshot )
        val deploymentId = status.studyDeploymentId
        val studyRuntime = StudyRuntime.initialize( deploymentService, deploymentId, smartphone.roleName, smartphone.createRegistration() )
        repo.addStudyRuntime( studyRuntime )

        // Make some changes and update.
        deploymentService.registerDevice( deploymentId, deviceSmartphoneDependsOn.roleName, SmartphoneDeviceRegistration( "dependent" ) )
        studyRuntime.tryDeployment( deploymentService )
        repo.updateStudyRuntime( studyRuntime )

        // Verify whether changes were stored.
        val retrievedRuntime = repo.getStudyRuntimeBy( deploymentId, smartphone.roleName )
        assertNotNull( retrievedRuntime )
        assertEquals( studyRuntime.getSnapshot(), retrievedRuntime.getSnapshot() )
    }

    @Test
    fun updateStudyRuntime_fails_for_unknown_runtime() = runBlockingTest {
        val (repo, deploymentService) = createDependencies()
        val deploymentId = addTestDeployment( deploymentService )
        val studyRuntime =
                StudyRuntime.initialize( deploymentService, deploymentId, smartphone.roleName, smartphone.createRegistration() )

        assertFailsWith<IllegalArgumentException> { repo.updateStudyRuntime( studyRuntime ) }
    }
}