package com.acme.transport.customs.inspection.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryInspectionProfileTest {
    @Test
    fun `starts in new`() {
        val item = newPrimaryInspectionProfile("code-670", "label-950")
        assertEquals(PrimaryInspectionProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newPrimaryInspectionProfile("code-670", "label-950")
        try {
            item.moveTo(PrimaryInspectionProfileStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryInspectionProfileStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newPrimaryInspectionProfile("code-670", "label-950")
        item.status = PrimaryInspectionProfileStatus.FAILED
        assertFails("move") { item.moveTo(PrimaryInspectionProfileStatus.NEW) }
    }
}
