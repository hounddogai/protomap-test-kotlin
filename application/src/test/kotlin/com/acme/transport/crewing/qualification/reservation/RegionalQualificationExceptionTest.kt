package com.acme.transport.crewing.qualification.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalQualificationExceptionTest {
    @Test
    fun `starts in draft`() {
        val item = newRegionalQualificationException("code-159", "label-854")
        assertEquals(RegionalQualificationExceptionStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRegionalQualificationException("code-159", "label-854")
        try {
            item.moveTo(RegionalQualificationExceptionStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalQualificationExceptionStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRegionalQualificationException("code-159", "label-854")
        item.status = RegionalQualificationExceptionStatus.CLOSED
        assertFails("move") { item.moveTo(RegionalQualificationExceptionStatus.DRAFT) }
    }
}
