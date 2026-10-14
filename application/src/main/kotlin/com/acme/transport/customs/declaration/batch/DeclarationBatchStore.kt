package com.acme.transport.customs.declaration.batch

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration batches by code. */
interface DeclarationBatchStore {
    fun find(code: String): DeclarationBatch?
    fun save(item: DeclarationBatch)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationBatch>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationBatchStore(
    var items: MutableMap<String, DeclarationBatch>,
    var order: MutableList<String>,
) : DeclarationBatchStore {
    override fun find(code: String): DeclarationBatch? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationBatch) {
        if (!items.containsKey(item.code)) {
            order.add(item.code)
        }
        items[item.code] = item
    }

    override fun remove(code: String): Boolean {
        if (!items.containsKey(code)) {
            return false
        }
        items.remove(code)
        val kept = mutableListOf<String>()
        for (existing in order) {
            if (existing != code) {
                kept.add(existing)
            }
        }
        order = kept
        return true
    }

    override fun list(): MutableList<DeclarationBatch> {
        val out = mutableListOf<DeclarationBatch>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationBatchStore(): DeclarationBatchStore {
    return MemoryDeclarationBatchStore(mutableMapOf<String, DeclarationBatch>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationBatchStore(
    var inner: DeclarationBatchStore,
    var cache: TtlCache<DeclarationBatch>,
    var hits: Int,
    var misses: Int,
) : DeclarationBatchStore {
    override fun find(code: String): DeclarationBatch? {
        val hit = (cache.get(code) as DeclarationBatch?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationBatch) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationBatch> = inner.list()
}

fun newCachedDeclarationBatchStore(inner: DeclarationBatchStore, ttl: Int, clock: Clock): DeclarationBatchStore {
    return CachedDeclarationBatchStore(
        inner = inner,
        cache = newTtlCache<DeclarationBatch>(1024, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration batches. Empty fields match all. */
data class DeclarationBatchQuery(
    var title: String,
    var minPallets: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationBatch): Boolean {
        if (title != "" && normalize(item.title) != normalize(title)) {
            return false
        }
        if (item.pallets < minPallets) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationBatch>): MutableList<DeclarationBatch> {
        val matched = filter(items) { candidate: DeclarationBatch -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationBatches(store: DeclarationBatchStore, query: DeclarationBatchQuery): MutableList<DeclarationBatch> {
    return query.apply(store.list())
}
