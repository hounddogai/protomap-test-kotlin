package com.acme.transport.crewing.restbreak.breakmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedRestBreakTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newWeightedRestBreakTemplate("code-636", "label-705")
        assertEquals(WeightedRestBreakTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newWeightedRestBreakTemplate("code-636", "label-705")
        try {
            item.moveTo(WeightedRestBreakTemplateStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedRestBreakTemplateStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newWeightedRestBreakTemplate("code-636", "label-705")
        item.status = WeightedRestBreakTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(WeightedRestBreakTemplateStatus.CREATED) }
    }
}
