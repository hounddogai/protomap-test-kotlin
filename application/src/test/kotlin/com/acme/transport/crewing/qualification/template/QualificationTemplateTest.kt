package com.acme.transport.crewing.qualification.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationTemplate("code-135", "label-126")
        assertEquals(QualificationTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newQualificationTemplate("code-135", "label-126")
        try {
            item.moveTo(QualificationTemplateStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationTemplateStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationTemplate("code-135", "label-126")
        item.status = QualificationTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationTemplateStatus.CREATED) }
    }
}
