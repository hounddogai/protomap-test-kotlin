package com.acme.transport.lastmile.parcel.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelTemplateStore()
        val item = newParcelTemplate("code-607", "name-158")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
