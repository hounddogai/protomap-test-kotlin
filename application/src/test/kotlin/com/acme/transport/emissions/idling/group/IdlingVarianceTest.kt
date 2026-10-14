package com.acme.transport.emissions.idling.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class IdlingVarianceTest {
    @Test
    fun `starts in open`() {
        val item = newIdlingVariance("code-419", "name-32")
        assertEquals(IdlingVarianceStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newIdlingVariance("code-419", "name-32")
        try {
            item.moveTo(IdlingVarianceStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(IdlingVarianceStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newIdlingVariance("code-419", "name-32")
        item.status = IdlingVarianceStatus.ARCHIVED
        assertFails("move") { item.moveTo(IdlingVarianceStatus.OPEN) }
    }
}
