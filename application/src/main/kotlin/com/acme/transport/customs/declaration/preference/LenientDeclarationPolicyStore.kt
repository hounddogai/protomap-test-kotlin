package com.acme.transport.customs.declaration.preference

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps lenient declaration policies by code. */
interface LenientDeclarationPolicyStore {
    fun find(code: String): LenientDeclarationPolicy?
    fun save(item: LenientDeclarationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<LenientDeclarationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryLenientDeclarationPolicyStore(
    var items: MutableMap<String, LenientDeclarationPolicy>,
    var order: MutableList<String>,
) : LenientDeclarationPolicyStore {
    override fun find(code: String): LenientDeclarationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: LenientDeclarationPolicy) {
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

    override fun list(): MutableList<LenientDeclarationPolicy> {
        val out = mutableListOf<LenientDeclarationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryLenientDeclarationPolicyStore(): LenientDeclarationPolicyStore {
    return MemoryLenientDeclarationPolicyStore(
        mutableMapOf<String, LenientDeclarationPolicy>(),
        mutableListOf<String>(),
    )
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedLenientDeclarationPolicyStore(
    var inner: LenientDeclarationPolicyStore,
    var cache: TtlCache<LenientDeclarationPolicy>,
    var hits: Int,
    var misses: Int,
) : LenientDeclarationPolicyStore {
    override fun find(code: String): LenientDeclarationPolicy? {
        val hit = (cache.get(code) as LenientDeclarationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: LenientDeclarationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<LenientDeclarationPolicy> = inner.list()
}

fun newCachedLenientDeclarationPolicyStore(inner: LenientDeclarationPolicyStore, ttl: Int, clock: Clock): LenientDeclarationPolicyStore {
    return CachedLenientDeclarationPolicyStore(
        inner = inner,
        cache = newTtlCache<LenientDeclarationPolicy>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects lenient declaration policies. Empty fields match all. */
data class LenientDeclarationPolicyQuery(
    var name: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: LenientDeclarationPolicy): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.endsAt < minEndsAt) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<LenientDeclarationPolicy>): MutableList<LenientDeclarationPolicy> {
        val matched = filter(items) { candidate: LenientDeclarationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findLenientDeclarationPolicies(store: LenientDeclarationPolicyStore, query: LenientDeclarationPolicyQuery): MutableList<LenientDeclarationPolicy> {
    return query.apply(store.list())
}
