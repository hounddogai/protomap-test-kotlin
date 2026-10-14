package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDefectTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newAdaptiveDefectTemplate("code-307", "title-360")
        assertEquals(AdaptiveDefectTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newAdaptiveDefectTemplate("code-307", "title-360")
        try {
            item.moveTo(AdaptiveDefectTemplateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDefectTemplateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newAdaptiveDefectTemplate("code-307", "title-360")
        item.status = AdaptiveDefectTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(AdaptiveDefectTemplateStatus.CREATED) }
    }
}
