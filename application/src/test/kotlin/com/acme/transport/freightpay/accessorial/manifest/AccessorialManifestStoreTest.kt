package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialManifestStore()
        val item = newAccessorialManifest("code-653", "title-535")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
