package com.acme.transport.customs.clearance.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ClearanceBaselineTest {
    @Test
    fun `starts in planned`() {
        val item = newClearanceBaseline("code-483", "title-632")
        assertEquals(ClearanceBaselineStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newClearanceBaseline("code-483", "title-632")
        try {
            item.moveTo(ClearanceBaselineStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ClearanceBaselineStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newClearanceBaseline("code-483", "title-632")
        item.status = ClearanceBaselineStatus.CANCELLED
        assertFails("move") { item.moveTo(ClearanceBaselineStatus.PLANNED) }
    }
}
