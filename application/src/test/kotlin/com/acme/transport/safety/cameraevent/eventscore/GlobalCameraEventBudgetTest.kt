package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalCameraEventBudgetTest {
    @Test
    fun `starts in created`() {
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        assertEquals(GlobalCameraEventBudgetStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        try {
            item.moveTo(GlobalCameraEventBudgetStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalCameraEventBudgetStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newGlobalCameraEventBudget("code-388", "label-986")
        item.status = GlobalCameraEventBudgetStatus.RETIRED
        assertFails("move") { item.moveTo(GlobalCameraEventBudgetStatus.CREATED) }
    }
}
