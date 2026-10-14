package com.acme.transport.customs.broker.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalBrokerVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newRegionalBrokerVariance("code-763", "title-792")
        assertEquals(RegionalBrokerVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRegionalBrokerVariance("code-763", "title-792")
        try {
            item.moveTo(RegionalBrokerVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalBrokerVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRegionalBrokerVariance("code-763", "title-792")
        item.status = RegionalBrokerVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(RegionalBrokerVarianceStatus.CREATED) }
    }
}
