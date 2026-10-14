package com.acme.transport.customs.declaration.snapshot

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps strict declaration baselines by code. */
interface StrictDeclarationBaselineStore {
    fun find(code: String): StrictDeclarationBaseline?
    fun save(item: StrictDeclarationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<StrictDeclarationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryStrictDeclarationBaselineStore(
    var items: MutableMap<String, StrictDeclarationBaseline>,
    var order: MutableList<String>,
) : StrictDeclarationBaselineStore {
    override fun find(code: String): StrictDeclarationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: StrictDeclarationBaseline) {
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

    override fun list(): MutableList<StrictDeclarationBaseline> {
        val out = mutableListOf<StrictDeclarationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryStrictDeclarationBaselineStore(): StrictDeclarationBaselineStore {
    return MemoryStrictDeclarationBaselineStore(
        mutableMapOf<String, StrictDeclarationBaseline>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedStrictDeclarationBaselineStore(
    var inner: StrictDeclarationBaselineStore,
    var cache: TtlCache<StrictDeclarationBaseline>,
    var hits: Int,
    var misses: Int,
) : StrictDeclarationBaselineStore {
    override fun find(code: String): StrictDeclarationBaseline? {
        val hit = (cache.get(code) as StrictDeclarationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: StrictDeclarationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<StrictDeclarationBaseline> = inner.list()
}

fun newCachedStrictDeclarationBaselineStore(inner: StrictDeclarationBaselineStore, ttl: Int, clock: Clock): StrictDeclarationBaselineStore {
    return CachedStrictDeclarationBaselineStore(
        inner = inner,
        cache = newTtlCache<StrictDeclarationBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects strict declaration baselines. Empty fields match all. */
data class StrictDeclarationBaselineQuery(
    var name: String,
    var minDiscount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: StrictDeclarationBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.discount < minDiscount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<StrictDeclarationBaseline>): MutableList<StrictDeclarationBaseline> {
        val matched = filter(items) { candidate: StrictDeclarationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findStrictDeclarationBaselines(store: StrictDeclarationBaselineStore, query: StrictDeclarationBaselineQuery): MutableList<StrictDeclarationBaseline> {
    return query.apply(store.list())
}
