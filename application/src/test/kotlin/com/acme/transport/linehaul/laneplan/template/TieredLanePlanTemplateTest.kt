package com.acme.transport.linehaul.laneplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredLanePlanTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newTieredLanePlanTemplate("code-792", "name-92")
        assertEquals(TieredLanePlanTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newTieredLanePlanTemplate("code-792", "name-92")
        try {
            item.moveTo(TieredLanePlanTemplateStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredLanePlanTemplateStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newTieredLanePlanTemplate("code-792", "name-92")
        item.status = TieredLanePlanTemplateStatus.FAILED
        assertFails("move") { item.moveTo(TieredLanePlanTemplateStatus.NEW) }
    }
}
