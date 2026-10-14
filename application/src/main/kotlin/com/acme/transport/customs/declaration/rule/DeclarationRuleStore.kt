package com.acme.transport.customs.declaration.rule

import com.acme.transport.customs.cache.TtlCache
import com.acme.transport.customs.cache.newTtlCache
import com.acme.transport.customs.clock.Clock
import com.acme.transport.kit.collections.filter
import com.acme.transport.kit.collections.take
import com.acme.transport.kit.text.normalize

/** Keeps declaration rules by code. */
interface DeclarationRuleStore {
    fun find(code: String): DeclarationRule?
    fun save(item: DeclarationRule)
    fun remove(code: String): Boolean
    fun list(): MutableList<DeclarationRule>
}

/** Keeps records in memory, in the order they were first saved. */
data class MemoryDeclarationRuleStore(
    var items: MutableMap<String, DeclarationRule>,
    var order: MutableList<String>,
) : DeclarationRuleStore {
    override fun find(code: String): DeclarationRule? {
        val found = items[code] ?: return null
        return found
    }

    override fun save(item: DeclarationRule) {
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

    override fun list(): MutableList<DeclarationRule> {
        val out = mutableListOf<DeclarationRule>()
        for (key in order) {
            val found = items[key] ?: continue
            out.add(found)
        }
        return out
    }
}

fun newMemoryDeclarationRuleStore(): DeclarationRuleStore {
    return MemoryDeclarationRuleStore(mutableMapOf<String, DeclarationRule>(), mutableListOf<String>())
}

/** Serves reads from a short-lived cache in front of another store. */
data class CachedDeclarationRuleStore(
    var inner: DeclarationRuleStore,
    var cache: TtlCache<DeclarationRule>,
    var hits: Int,
    var misses: Int,
) : DeclarationRuleStore {
    override fun find(code: String): DeclarationRule? {
        val hit = (cache.get(code) as DeclarationRule?)
        if (hit == null) {
            misses += 1
            val found = inner.find(code) ?: return null
            cache.put(code, found)
            return found
        }
        hits += 1
        return hit
    }

    override fun save(item: DeclarationRule) {
        inner.save(item)
        cache.put(item.code, item)
    }

    override fun remove(code: String): Boolean {
        // A cached copy expires on its own; reads after a removal may still see it until then.
        return inner.remove(code)
    }

    override fun list(): MutableList<DeclarationRule> = inner.list()
}

fun newCachedDeclarationRuleStore(inner: DeclarationRuleStore, ttl: Int, clock: Clock): DeclarationRuleStore {
    return CachedDeclarationRuleStore(
        inner = inner,
        cache = newTtlCache<DeclarationRule>(256, ttl, clock),
        hits = 0,
        misses = 0,
    )
}

/** Selects declaration rules. Empty fields match all. */
data class DeclarationRuleQuery(
    var name: String,
    var minAttempts: Int,
    var statuses: MutableList<String>,
    var limit: Int,
) {
    fun matches(item: DeclarationRule): Boolean {
        if (name != "" && normalize(item.name) != normalize(name)) {
            return false
        }
        if (item.attempts < minAttempts) {
            return false
        }
        if (statuses.size > 0 && !statuses.contains("${item.status}")) {
            return false
        }
        return true
    }

    fun apply(items: MutableList<DeclarationRule>): MutableList<DeclarationRule> {
        val matched = filter(items) { candidate: DeclarationRule -> matches(candidate) }
        if (limit > 0 && matched.size > limit) {
            return take(matched, limit)
        }
        return matched
    }
}

fun findDeclarationRules(store: DeclarationRuleStore, query: DeclarationRuleQuery): MutableList<DeclarationRule> {
    return query.apply(store.list())
}
