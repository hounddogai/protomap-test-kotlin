package com.acme.transport.fleetops.tire.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalTireBatchTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalTireBatch("code-341", "label-681")
        assertEquals(RegionalTireBatchStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalTireBatch("code-341", "label-681")
        try {
            item.moveTo(RegionalTireBatchStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalTireBatchStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalTireBatch("code-341", "label-681")
        item.status = RegionalTireBatchStatus.FAILED
        assertFails("move") { item.moveTo(RegionalTireBatchStatus.NEW) }
    }
}
