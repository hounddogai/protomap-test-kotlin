package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationCycleTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationCycle("code-217", "title-291")
        assertEquals(QualificationCycleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newQualificationCycle("code-217", "title-291")
        try {
            item.moveTo(QualificationCycleStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationCycleStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationCycle("code-217", "title-291")
        item.status = QualificationCycleStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationCycleStatus.CREATED) }
    }
}
