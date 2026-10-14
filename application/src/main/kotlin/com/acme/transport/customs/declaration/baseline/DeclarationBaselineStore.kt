package com.acme.transport.customs.declaration.baseline

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration baselines by code. */
interface DeclarationBaselineStore {
    fun find(code: String): DeclarationBaseline?
    fun save(item: DeclarationBaseline)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationBaseline>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationBaselineStore(
    var items: MutableMap<String, DeclarationBaseline>,
    var order: MutableList<String>,
) : DeclarationBaselineStore {
    override fun find(code: String): DeclarationBaseline? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationBaseline) {
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

    override fun list(): MutableList<DeclarationBaseline> {
        val out = mutableListOf<DeclarationBaseline>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationBaselineStore(): DeclarationBaselineStore {
    return MemoryDeclarationBaselineStore(mutableMapOf<String, DeclarationBaseline>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationBaselineStore(
    var inner: DeclarationBaselineStore,
    var cache: TtlCache<DeclarationBaseline>,
    var hits: Int,
    var misses: Int,
) : DeclarationBaselineStore {
    override fun find(code: String): DeclarationBaseline? {
        val hit = (cache.get(code) as DeclarationBaseline?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationBaseline) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationBaseline> = inner.list()
}

fun newCachedDeclarationBaselineStore(inner: DeclarationBaselineStore, ttl: Int, clock: Clock): DeclarationBaselineStore {
    return CachedDeclarationBaselineStore(
        inner = inner,
        cache = newTtlCache<DeclarationBaseline>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration baselines. Empty fields match all. */
data class DeclarationBaselineQuery(
    var name: String,
    var minAmount: Double,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationBaseline): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.amount < minAmount) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationBaseline>): MutableList<DeclarationBaseline> {
        val matched = filter(items) { candidate: DeclarationBaseline -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationBaselines(store: DeclarationBaselineStore, query: DeclarationBaselineQuery): MutableList<DeclarationBaseline> {
    return query.apply(store.list())
}
