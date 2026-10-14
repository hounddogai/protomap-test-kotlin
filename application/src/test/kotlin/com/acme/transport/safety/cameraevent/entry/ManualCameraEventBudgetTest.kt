package com.acme.transport.safety.cameraevent.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventBudgetTest {
    @Test
    fun `starts in open`() {
        val item = newManualCameraEventBudget("code-174", "title-2")
        assertEquals(ManualCameraEventBudgetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newManualCameraEventBudget("code-174", "title-2")
        try {
            item.moveTo(ManualCameraEventBudgetStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventBudgetStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualCameraEventBudget("code-174", "title-2")
        item.status = ManualCameraEventBudgetStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualCameraEventBudgetStatus.OPEN) }
    }
}
