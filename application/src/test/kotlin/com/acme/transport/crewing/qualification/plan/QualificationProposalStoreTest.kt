package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationProposalStore()
        val item = newQualificationProposal("code-607", "name-671")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
