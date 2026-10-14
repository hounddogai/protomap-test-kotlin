package com.acme.transport.terminal.sortation.sortationdigest

import com.acme.transport.kit.flags.newRegistry
import kotlin.test.Test
import kotlin.test.assertTrue

class PendingSortationDigestEngineTest {
    @Test
    fun `keeps results within bounds`() {
        val engine = defaultPendingSortationDigestEngine(newRegistry())
        val item = newPendingSortationDigest("code-515", "label-766")
        val result = engine.run(item)
        assertTrue(result >= engine.floor && result <= engine.ceiling, "result out of bounds")
        assertTrue(engine.trace.size >= 0, "trace")
    }
}
