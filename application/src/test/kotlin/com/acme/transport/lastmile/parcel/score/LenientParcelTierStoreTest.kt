package com.acme.transport.lastmile.parcel.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientParcelTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientParcelTierStore()
        val item = newLenientParcelTier("code-991", "title-278")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
