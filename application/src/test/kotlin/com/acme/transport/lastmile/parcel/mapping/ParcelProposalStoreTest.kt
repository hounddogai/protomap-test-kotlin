package com.acme.transport.lastmile.parcel.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelProposalStore()
        val item = newParcelProposal("code-691", "title-298")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
