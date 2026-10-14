package com.acme.transport.crewing.qualification.qualificationdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newQualificationWindow("code-166", "name-682")
        assertEquals(QualificationWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newQualificationWindow("code-166", "name-682")
        try {
            item.moveTo(QualificationWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newQualificationWindow("code-166", "name-682")
        item.status = QualificationWindowStatus.CLOSED
        assertFails("move") { item.moveTo(QualificationWindowStatus.DRAFT) }
    }
}
