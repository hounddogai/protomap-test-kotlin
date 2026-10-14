package com.acme.transport.fleetops.defect.defectprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDefectProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newGlobalDefectProfile("code-310", "name-460")
        assertEquals(GlobalDefectProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newGlobalDefectProfile("code-310", "name-460")
        try {
            item.moveTo(GlobalDefectProfileStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDefectProfileStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newGlobalDefectProfile("code-310", "name-460")
        item.status = GlobalDefectProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(GlobalDefectProfileStatus.REQUESTED) }
    }
}
