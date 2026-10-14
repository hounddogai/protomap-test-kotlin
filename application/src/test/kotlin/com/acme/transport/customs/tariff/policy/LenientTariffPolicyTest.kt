package com.acme.transport.customs.tariff.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientTariffPolicyTest {
    @Test
    fun `starts in open`() {
        val item = newLenientTariffPolicy("code-982", "label-409")
        assertEquals(LenientTariffPolicyStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newLenientTariffPolicy("code-982", "label-409")
        try {
            item.moveTo(LenientTariffPolicyStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientTariffPolicyStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLenientTariffPolicy("code-982", "label-409")
        item.status = LenientTariffPolicyStatus.ARCHIVED
        assertFails("move") { item.moveTo(LenientTariffPolicyStatus.OPEN) }
    }
}
