package com.acme.transport.customs.declaration.assignment

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration policies by code. */
interface DeclarationPolicyStore {
    fun find(code: String): DeclarationPolicy?
    fun save(item: DeclarationPolicy)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationPolicy>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationPolicyStore(
    var items: MutableMap<String, DeclarationPolicy>,
    var order: MutableList<String>,
) : DeclarationPolicyStore {
    override fun find(code: String): DeclarationPolicy? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationPolicy) {
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

    override fun list(): MutableList<DeclarationPolicy> {
        val out = mutableListOf<DeclarationPolicy>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationPolicyStore(): DeclarationPolicyStore {
    return MemoryDeclarationPolicyStore(mutableMapOf<String, DeclarationPolicy>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationPolicyStore(
    var inner: DeclarationPolicyStore,
    var cache: TtlCache<DeclarationPolicy>,
    var hits: Int,
    var misses: Int,
) : DeclarationPolicyStore {
    override fun find(code: String): DeclarationPolicy? {
        val hit = (cache.get(code) as DeclarationPolicy?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationPolicy) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationPolicy> = inner.list()
}

fun newCachedDeclarationPolicyStore(inner: DeclarationPolicyStore, ttl: Int, clock: Clock): DeclarationPolicyStore {
    return CachedDeclarationPolicyStore(
        inner = inner,
        cache = newTtlCache<DeclarationPolicy>(64, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration policies. Empty fields match all. */
data class DeclarationPolicyQuery(
    var label: String,
    var minEndsAt: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationPolicy): Boolean {
        if (label != "" && normalize(item.label) != normalize(label)) {
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

    fun apply(items: MutableList<DeclarationPolicy>): MutableList<DeclarationPolicy> {
        val matched = filter(items) { candidate: DeclarationPolicy -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationPolicies(store: DeclarationPolicyStore, query: DeclarationPolicyQuery): MutableList<DeclarationPolicy> {
    return query.apply(store.list())
}
