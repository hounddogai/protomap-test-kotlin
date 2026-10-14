package com.acme.transport.freightpay.ratecard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveRateCardTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveRateCardTemplate("code-545", "title-644")
        assertEquals(AdaptiveRateCardTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newAdaptiveRateCardTemplate("code-545", "title-644")
        try {
            item.moveTo(AdaptiveRateCardTemplateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveRateCardTemplateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveRateCardTemplate("code-545", "title-644")
        item.status = AdaptiveRateCardTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveRateCardTemplateStatus.CREATED) }
    }
}
