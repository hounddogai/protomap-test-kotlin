package com.acme.transport.network.servicearea.manualareathreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalServiceAreaTemplateTest {
    @Test
    fun `starts in planned`() {
        val item = newRegionalServiceAreaTemplate("code-732", "label-616")
        assertEquals(RegionalServiceAreaTemplateStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newRegionalServiceAreaTemplate("code-732", "label-616")
        try {
            item.moveTo(RegionalServiceAreaTemplateStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalServiceAreaTemplateStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newRegionalServiceAreaTemplate("code-732", "label-616")
        item.status = RegionalServiceAreaTemplateStatus.CANCELLED
        assertFails("move") { item.moveTo(RegionalServiceAreaTemplateStatus.PLANNED) }
    }
}
