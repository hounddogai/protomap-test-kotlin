package com.acme.transport.network.servicearea.primaryareatarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedServiceAreaVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedServiceAreaVariance("code-560", "label-899")
        assertEquals(CappedServiceAreaVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newCappedServiceAreaVariance("code-560", "label-899")
        try {
            item.moveTo(CappedServiceAreaVarianceStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedServiceAreaVarianceStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedServiceAreaVariance("code-560", "label-899")
        item.status = CappedServiceAreaVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(CappedServiceAreaVarianceStatus.IDLE) }
    }
}
