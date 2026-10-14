package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRedeliveryWindowTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedRedeliveryWindow("code-827", "title-462")
        assertEquals(WeightedRedeliveryWindowStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newWeightedRedeliveryWindow("code-827", "title-462")
        try {
            item.moveTo(WeightedRedeliveryWindowStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRedeliveryWindowStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedRedeliveryWindow("code-827", "title-462")
        item.status = WeightedRedeliveryWindowStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedRedeliveryWindowStatus.CREATED) }
    }
}
