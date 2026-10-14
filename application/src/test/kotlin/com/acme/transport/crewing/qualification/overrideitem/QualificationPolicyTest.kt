package com.acme.transport.crewing.qualification.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationPolicyTest {
    @Test
    fun `starts in idle`() {
        val item = newQualificationPolicy("code-290", "label-118")
        assertEquals(QualificationPolicyStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to stopped`() {
        val item = newQualificationPolicy("code-290", "label-118")
        try {
            item.moveTo(QualificationPolicyStatus.STOPPED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationPolicyStatus.STOPPED, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newQualificationPolicy("code-290", "label-118")
        item.status = QualificationPolicyStatus.STOPPED
        assertFails("move") { item.moveTo(QualificationPolicyStatus.IDLE) }
    }
}
