package com.acme.transport.freightpay.accessorial.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedAccessorialTemplateTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedAccessorialTemplate("code-505", "name-731")
        assertEquals(WeightedAccessorialTemplateStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newWeightedAccessorialTemplate("code-505", "name-731")
        try {
            item.moveTo(WeightedAccessorialTemplateStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedAccessorialTemplateStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedAccessorialTemplate("code-505", "name-731")
        item.status = WeightedAccessorialTemplateStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedAccessorialTemplateStatus.DRAFT) }
    }
}
