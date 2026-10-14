package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalParcelProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalParcelProfileStore()
        val item = newGlobalParcelProfile("code-247", "label-600")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
