package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryQualificationRecordTest {
    @Test
    fun `starts in planned`() {
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        assertEquals(PrimaryQualificationRecordStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        try {
            item.moveTo(PrimaryQualificationRecordStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryQualificationRecordStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPrimaryQualificationRecord("code-409", "label-921")
        item.status = PrimaryQualificationRecordStatus.CANCELLED
        assertFails("move") { item.moveTo(PrimaryQualificationRecordStatus.PLANNED) }
    }
}
