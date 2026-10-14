package com.acme.transport.freightpay.ratecard.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualRateCardPreferenceTest {
    @Test
    fun `starts in open`() {
        val item = newManualRateCardPreference("code-695", "label-670")
        assertEquals(ManualRateCardPreferenceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newManualRateCardPreference("code-695", "label-670")
        try {
            item.moveTo(ManualRateCardPreferenceStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualRateCardPreferenceStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newManualRateCardPreference("code-695", "label-670")
        item.status = ManualRateCardPreferenceStatus.ARCHIVED
        assertFails("move") { item.moveTo(ManualRateCardPreferenceStatus.OPEN) }
    }
}
