package com.acme.transport.fleetops.defect.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectRollupTest {
    @Test
    fun `starts in requested`() {
        val item = newDefectRollup("code-443", "name-521")
        assertEquals(DefectRollupStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefectRollup("code-443", "name-521")
        try {
            item.moveTo(DefectRollupStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectRollupStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefectRollup("code-443", "name-521")
        item.status = DefectRollupStatus.EXPIRED
        assertFails("move") { item.moveTo(DefectRollupStatus.REQUESTED) }
    }
}
