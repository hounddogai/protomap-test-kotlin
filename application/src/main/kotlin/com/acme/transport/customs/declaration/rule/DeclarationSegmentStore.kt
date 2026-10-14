package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration segments by code. */
interface DeclarationSegmentStore {
    fun find(code: String): DeclarationSegment?
    fun save(item: DeclarationSegment)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationSegment>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationSegmentStore(
    var items: MutableMap<String, DeclarationSegment>,
    var order: MutableList<String>,
) : DeclarationSegmentStore {
    override fun find(code: String): DeclarationSegment? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationSegment) {
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

    override fun list(): MutableList<DeclarationSegment> {
        val out = mutableListOf<DeclarationSegment>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationSegmentStore(): DeclarationSegmentStore {
    return MemoryDeclarationSegmentStore(mutableMapOf<String, DeclarationSegment>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationSegmentStore(
    var inner: DeclarationSegmentStore,
    var cache: TtlCache<DeclarationSegment>,
    var hits: Int,
    var misses: Int,
) : DeclarationSegmentStore {
    override fun find(code: String): DeclarationSegment? {
        val hit = (cache.get(code) as DeclarationSegment?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationSegment) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationSegment> = inner.list()
}

fun newCachedDeclarationSegmentStore(inner: DeclarationSegmentStore, ttl: Int, clock: Clock): DeclarationSegmentStore {
    return CachedDeclarationSegmentStore(
        inner = inner,
        cache = newTtlCache<DeclarationSegment>(128, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration segments. Empty fields match all. */
data class DeclarationSegmentQuery(
    var label: String,
    var minLoadKg: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationSegment): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
            return false
        }
        if (item.loadKg < minLoadKg) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationSegment>): MutableList<DeclarationSegment> {
        val matched = filter(items) { candidate: DeclarationSegment -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationSegments(store: DeclarationSegmentStore, query: DeclarationSegmentQuery): MutableList<DeclarationSegment> {
    return query.apply(store.list())
}
