package com.acme.transport.lastmile.parcel.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultParcelExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultParcelExceptionStore()
        val item = newDefaultParcelException("code-300", "label-641")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
