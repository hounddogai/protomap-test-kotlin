package com.acme.transport.freightpay.accessorial.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AccessorialRuleTest {
    @Test
    fun `starts in planned`() {
        val item = newAccessorialRule("code-47", "title-549")
        assertEquals(AccessorialRuleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAccessorialRule("code-47", "title-549")
        try {
            item.moveTo(AccessorialRuleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AccessorialRuleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAccessorialRule("code-47", "title-549")
        item.status = AccessorialRuleStatus.CANCELLED
        assertFails("move") { item.moveTo(AccessorialRuleStatus.PLANNED) }
    }
}
