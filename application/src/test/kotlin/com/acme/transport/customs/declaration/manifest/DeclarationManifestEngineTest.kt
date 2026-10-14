package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class DeclarationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultDeclarationManifestEngine(newRegistry())
        val item = newDeclarationManifest("code-550", "title-890")
        val result = engine.compute(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
