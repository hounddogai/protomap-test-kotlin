package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class EvChargeBaselineTest {
    @Test
    fun `starts in created`() {
        val item = newEvChargeBaseline("code-489", "label-307")
        assertEquals(EvChargeBaselineStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newEvChargeBaseline("code-489", "label-307")
        try {
            item.moveTo(EvChargeBaselineStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(EvChargeBaselineStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newEvChargeBaseline("code-489", "label-307")
        item.status = EvChargeBaselineStatus.RETIRED
        assertFails("move") { item.moveTo(EvChargeBaselineStatus.CREATED) }
    }
}
