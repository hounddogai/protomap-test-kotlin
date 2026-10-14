package com.acme.transport.customs.broker.brokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalBrokerEstimateTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalBrokerEstimate("code-786", "title-842")
        assertEquals(SeasonalBrokerEstimateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newSeasonalBrokerEstimate("code-786", "title-842")
        try {
            item.moveTo(SeasonalBrokerEstimateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalBrokerEstimateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalBrokerEstimate("code-786", "title-842")
        item.status = SeasonalBrokerEstimateStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalBrokerEstimateStatus.CREATED) }
    }
}
