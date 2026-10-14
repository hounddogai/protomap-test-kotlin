package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardManifestTest {
    @Test
    fun `starts in created`() {
        val item = newRateCardManifest("code-593", "name-796")
        assertEquals(RateCardManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRateCardManifest("code-593", "name-796")
        try {
            item.moveTo(RateCardManifestStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardManifestStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRateCardManifest("code-593", "name-796")
        item.status = RateCardManifestStatus.RETIRED
        assertFails("move") { item.moveTo(RateCardManifestStatus.CREATED) }
    }
}
