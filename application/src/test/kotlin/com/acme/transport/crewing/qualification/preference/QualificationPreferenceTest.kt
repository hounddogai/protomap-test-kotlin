package com.acme.transport.crewing.qualification.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationPreferenceTest {
    @Test
    fun `starts in idle`() {
        val item = newQualificationPreference("code-642", "label-655")
        assertEquals(QualificationPreferenceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newQualificationPreference("code-642", "label-655")
        try {
            item.moveTo(QualificationPreferenceStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationPreferenceStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newQualificationPreference("code-642", "label-655")
        item.status = QualificationPreferenceStatus.STOPPED
        assertFails("move") { item.moveTo(QualificationPreferenceStatus.IDLE) }
    }
}
