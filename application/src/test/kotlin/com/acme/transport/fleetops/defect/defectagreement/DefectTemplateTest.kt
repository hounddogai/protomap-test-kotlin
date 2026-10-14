package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefectTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefectTemplate("code-236", "name-707")
        assertEquals(DefectTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDefectTemplate("code-236", "name-707")
        try {
            item.moveTo(DefectTemplateStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefectTemplateStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefectTemplate("code-236", "name-707")
        item.status = DefectTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefectTemplateStatus.PROPOSED) }
    }
}
