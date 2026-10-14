package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class QualificationSegmentTest {
    @Test
    fun `starts in created`() {
        val item = newQualificationSegment("code-698", "name-847")
        assertEquals(QualificationSegmentStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newQualificationSegment("code-698", "name-847")
        try {
            item.moveTo(QualificationSegmentStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(QualificationSegmentStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newQualificationSegment("code-698", "name-847")
        item.status = QualificationSegmentStatus.RETIRED
        assertFails("move") { item.moveTo(QualificationSegmentStatus.CREATED) }
    }
}
