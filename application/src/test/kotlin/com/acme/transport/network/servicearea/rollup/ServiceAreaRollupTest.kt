package com.acme.transport.network.servicearea.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaRollupTest {
    @Test
    fun `starts in draft`() {
        val item = newServiceAreaRollup("code-808", "label-37")
        assertEquals(ServiceAreaRollupStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newServiceAreaRollup("code-808", "label-37")
        try {
            item.moveTo(ServiceAreaRollupStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaRollupStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newServiceAreaRollup("code-808", "label-37")
        item.status = ServiceAreaRollupStatus.CLOSED
        assertFails("move") { item.moveTo(ServiceAreaRollupStatus.DRAFT) }
    }
}
