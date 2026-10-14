package com.acme.transport.crewing.qualification.qualificationoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationProfileTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationProfile("code-720", "name-810")
        assertEquals(QualificationProfileStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newQualificationProfile("code-720", "name-810")
        try {
            item.moveTo(QualificationProfileStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationProfileStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationProfile("code-720", "name-810")
        item.status = QualificationProfileStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationProfileStatus.CREATED) }
    }
}
