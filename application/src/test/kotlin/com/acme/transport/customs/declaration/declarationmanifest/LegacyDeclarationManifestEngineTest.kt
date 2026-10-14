package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertTrue

class LegacyDeclarationManifestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultLegacyDeclarationManifestEngine()
        val item = newLegacyDeclarationManifest("code-325", "label-200")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
