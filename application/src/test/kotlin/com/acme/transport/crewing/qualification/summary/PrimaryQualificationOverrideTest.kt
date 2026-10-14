package com.acme.transport.crewing.qualification.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryQualificationOverrideTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryQualificationOverride("code-926", "label-24")
        assertEquals(PrimaryQualificationOverrideStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPrimaryQualificationOverride("code-926", "label-24")
        try {
            item.moveTo(PrimaryQualificationOverrideStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryQualificationOverrideStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryQualificationOverride("code-926", "label-24")
        item.status = PrimaryQualificationOverrideStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryQualificationOverrideStatus.PLANNED) }
    }
}
