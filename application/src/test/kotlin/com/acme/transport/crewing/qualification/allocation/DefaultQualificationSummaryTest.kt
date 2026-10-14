package com.acme.transport.crewing.qualification.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultQualificationSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newDefaultQualificationSummary("code-358", "label-484")
        assertEquals(DefaultQualificationSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newDefaultQualificationSummary("code-358", "label-484")
        try {
            item.moveTo(DefaultQualificationSummaryStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultQualificationSummaryStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDefaultQualificationSummary("code-358", "label-484")
        item.status = DefaultQualificationSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(DefaultQualificationSummaryStatus.DRAFT) }
    }
}
