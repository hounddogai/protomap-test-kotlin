package com.acme.transport.linehaul.trailer.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTrailerCheckpoint("code-141", "name-532")
        assertEquals(RollingTrailerCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingTrailerCheckpoint("code-141", "name-532")
        try {
            item.moveTo(RollingTrailerCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTrailerCheckpoint("code-141", "name-532")
        item.status = RollingTrailerCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTrailerCheckpointStatus.CREATED) }
    }
}
